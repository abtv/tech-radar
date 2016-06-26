(ns tech-radar.analytics.search)

(def word #"[#]?[\w\-]+")

(defn new-search []
  (atom {:texts (sorted-map)
         :index {}}))

(defn- add-to-texts [search {:keys [id text twitter-id created-at]}]
  (assoc-in search [:texts id] {:text       text
                                :twitter-id twitter-id
                                :created-at created-at}))

(defn- add-to-index [search id topics words]
  (let [conj-ids     (fn [ids]
                       (if ids
                         (conj ids id)
                         [id]))
        words-reduce (fn [search word]
                       (reduce (fn [search topic]
                                 (update-in search [:index topic word] conj-ids)) search topics))]
    (reduce words-reduce search words)))

(defn- get-words [^String text]
  (->> (.toLowerCase text)
       (re-seq word)
       (distinct)))

(defn add-text [search {:keys [id text topics] :as orig}]
  (let [words (get-words text)]
    (swap! search (fn [search]
                    (-> search
                        (add-to-texts orig)
                        (add-to-index id topics words))))))

(defn- intersect-sorted-vectors [x y]
  (let [count-x (count x)
        count-y (count y)]
    (loop [i (int 0)
           j (int 0)
           r (transient [])]
      (if (and (< i count-x) (< j count-y))
        (let [xi (nth x i nil)
              yj (nth y j nil)]
          (cond
            (not (or xi yj)) (persistent! r)
            (< xi yj) (recur (inc i) j r)
            (> xi yj) (recur i (inc j) r)
            :else (recur (inc i) (inc j) (conj! r xi))))
        (persistent! r)))))

(defn- merge-ids
  ([idx]
   idx)
  ([idx1 idx2]
   (intersect-sorted-vectors idx1 idx2))
  ([idx1 idx2 idx3]
   (let [idx (merge-ids idx1 idx2)]
     (merge-ids idx idx3))))

(defn- take-last-vec [n v]
  (let [size (count v)]
    (if (<= size n)
      v
      (subvec v (- size n)))))

(defn to-result [ids]
  {:total (count ids)
   :ids   (take-last-vec 100 ids)})

(defn- get-ids [search topic words]
  (->> words
       (take 3)
       (mapv (fn [word]
               (get-in search [:index topic word])))
       (sort-by count)
       (apply merge-ids)
       (to-result)))

(defn search-texts [search topic text]
  (let [words  (get-words text)
        search @search
        {:keys [total ids]} (get-ids search topic words)
        texts  (get-in search [:texts])]
    {:total total
     :texts (reduce (fn [acc id]
                      (let [obj (get texts id)]
                        (conj acc (assoc obj :id id)))) [] ids)}))

(defn index-info [search]
  (let [texts      (get-in @search [:texts])
        first-text (second (first texts))
        last-text  (second (last texts))]
    {:texts-count (count texts)
     :latest      (:created-at first-text)
     :newest      (:created-at last-text)}))

(defn remove-oldest-item [search]
  (let [dsearch     @search
        words       (-> (:texts dsearch)
                        (first)
                        (second)
                        (:text)
                        (get-words))
        id          (-> (:texts dsearch)
                        (first)
                        (first))
        remove-text (fn [search]
                      (let [texts (-> (:texts search)
                                      (dissoc id))]
                        (assoc search :texts texts)))
        topic-fn    (fn [topic]
                      (fn [search word]
                        (let [idx (get-in search [:index topic word])]
                          (if (and idx (= id (first idx)))
                            (let [idx (subvec idx 1)]
                              (if (seq idx)
                                (assoc-in search [:index topic word] idx)
                                (let [topic-map (get-in search [:index topic])]
                                  (assoc-in search [:index topic] (dissoc topic-map word)))))
                            search))))]
    (swap! search (fn [search]
                    (let [topics (-> (:index search)
                                     (keys))
                          search (remove-text search)]
                      (reduce (fn [search topic]
                                (reduce (topic-fn topic) search words)) search topics))))))
