# 20170609_GeekOut_Something_IoT
Something, Something and IoT


## preparations

### cockroachdb
```docker
docker pull cockroachdb/cockroach:v1.0
```

Make sure the CockroachDB executable works:
```docker
docker run --rm cockroachdb/cockroach:v1.0 version
```

start network

```docker
docker network create -d bridge roachnet
```

cd _data/docker

Now start first node
```docker
docker run -d \
--name=roach1 \
--hostname=roach1 \
--net=roachnet \
-p 26257:26257 -p 8080:8080  \
-v "${PWD}/cockroach-data/roach1:/cockroach/cockroach-data"  \
cockroachdb/cockroach:v1.0 start --insecure
```

Start now more nodes

```docker
docker run -d \
--name=roach2 \
--hostname=roach2 \
--net=roachnet \
-v "${PWD}/cockroach-data/roach2:/cockroach/cockroach-data" \
cockroachdb/cockroach:v1.0 start --insecure --join=roach1
```

```docker
docker run -d \
--name=roach3 \
--hostname=roach3 \
--net=roachnet \
-v "${PWD}/cockroach-data/roach3:/cockroach/cockroach-data" \
cockroachdb/cockroach:v1.0 start --insecure --join=roach1
```

Now that you’ve scaled to 3 nodes, 
you can use any node as a SQL gateway to the cluster. 

The default replica count is 3, so I added 2 nodes more....

```docker
docker run -d \
--name=roach4 \
--hostname=roach4 \
--net=roachnet \
-v "${PWD}/cockroach-data/roach4:/cockroach/cockroach-data" \
cockroachdb/cockroach:v1.0 start --insecure --join=roach1
```

```docker
docker run -d \
--name=roach5 \
--hostname=roach5 \
--net=roachnet \
-v "${PWD}/cockroach-data/roach5:/cockroach/cockroach-data" \
cockroachdb/cockroach:v1.0 start --insecure --join=roach1
```

```docker
docker run roach1 roach2 roach3 roach4 roach5
```

Later you can stop and remove the nodes.
Stop the containers:

```docker
docker stop roach1 roach2 roach3 roach4 roach5
```

Remove the containers:

```docker
docker rm roach1 roach2 roach3 roach4 roach5
```

#### AdminUI

[http://localhost:8080](http://localhost:8080)

#### Usermanagement and Co

#### SQL Commands

SHOW DATABASES

set default db 

```postgresql
CREATE DATABASE geekoutdb;
SHOW DATABASES;
SET DATABASE = geekoutdb;

CREATE TABLE IF NOT EXISTS accounts (
    id INT PRIMARY KEY,
    balance DECIMAL
);

INSERT INTO IF NOT EXISTS geekoutdb.logins
(username, activated, passwd)
VALUES ( 'sven.ruppert', true, 'passwd')
```