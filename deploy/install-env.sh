sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update -y
sudo apt-get install language-pack-ru -y
sudo apt-get install htop -y
sudo apt-get install zip -y
sudo apt-get install unzip -y
echo debconf shared/accepted-oracle-license-v1-1 select true | \
sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | \
sudo debconf-set-selections
sudo apt-get install oracle-java8-installer -y
sudo apt-get install postgresql -y
su - postgres -c "psql -U postgres -d postgres -c \"alter user postgres with password 'postgres';\""
sudo -i -u postgres createdb -U postgres analytics
sudo apt-get install nginx -y
sudo apt-get install git -y
sudo wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
sudo chmod u+x lein
sudo LEIN_ROOT=true ./lein
sudo ufw default deny incoming
sudo ufw default allow outcoming
sudo ufw allow ssh
sudo ufw allow http
sudo ufw allow 3000
sudo ufw enable -y
