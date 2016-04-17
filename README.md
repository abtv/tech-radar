# tech-radar

## 1. Prerequisites
* OpenJDK 7 or Oracle JDK 8
* Leiningen 2.5 or later
* Git
* PostgreSQL 9.4 or later

## 2. PostgreSQL settings

Default database is `tech_radar`, default user is `postgres`, default password is `postgres`. 
You can change these settings with `database` parameter in `project.clj` file (development) and `.lein-env` file (production).

## 3. Twitter security settings

`tech-radar` receives data from Twitter stream. You need to create a file called `twitter-security.edn` 
in the project folder with the following content:

```
{:app-key           "your app key"
 :app-secret        "your app secret"
 :user-token        "your user token"
 :user-token-secret "your user token secret"}
```

## 4. Build from sources

Clone `tech-radar` repository

```
git clone git://github.com/abtv/tech-radar.git
```

Build with `build-release-all.sh` script 

```
./build-release-all.sh
```

`frontend-release` folder contains frontend application
`backend-release` folder contains backend application and all the settings and security files

## 5. Deployment to Ubuntu 14.04 server

1. `deploy/install-env.sh` file contains everything you need to setup environment for tech-radar under Ubuntu server
2. put `deploy/tech-radar.conf` file to `/etc/init/` folder
3. copy `backend-release` folder to your server

## It's not perfect... yet
Backend is mostly ok, but frontend is just a very early implementation. I'm going to use Om.next for frontend. 
I would appreciate any help.

## License

Copyright (c) Andrey Butov. All rights reserved. The use and
distribution terms for this software are covered by the Eclipse
Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this
distribution. By using this software in any fashion, you are
agreeing to be bound by the terms of this license. You must
not remove this notice, or any other, from this software.
