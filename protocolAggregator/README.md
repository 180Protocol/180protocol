## Conclave Sample

This is a simple app using the Conclave API. It is licensed under the Apache 2 license, and therefore you may 
copy/paste it to act as the basis of your own commercial or open source apps.


# How to build 

1. Make sure enclave build.gradle has runtime specified to 'avian'
2. Build using 
```
gradlew.bat host:assemble
```

# How to run enclave locally (Docker)
1. Start docker client

2. Install dist
```
gradlew.bat host:installDist
```

2. Run below, noting the port bindings for the four clients 

```
docker run -it --rm -p 9999:9999 -v ${PWD}:/project -w /project conclave-build /bin/bash
```

3. Run the enclave app from within the container 

```
cd host/build/install
./host/bin/host
```

## Run client code

1. Single invokation of client will launch different client role in sequance.

```
.\gradlew client:run 
```

# How to run

Start the host on a Linux system, which will build the enclave and host:

```
./gradlew host:run
```

It should print out some info about the started enclave. Then you can use the client to send mails in sequance:

```
./gradlew client:run 
```
