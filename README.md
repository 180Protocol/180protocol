## Running 180ProtocolPOC in Docker

1. You need to run build command to build your corda app which generate build folder inside pocCordApp. After that you
   have run deployNodes command to generate nodes folder inside build folder.

2. After that, You have to run below command from the project root path to start 180Dashboard service & pocCordApp
   service in docker container

   "docker-compose -f ./compose-corda-network.yml -f ./compose-cordaptor.yml up"

3. You can run http://localhost:3000 on browser to access your react dashboard.
4. You can login using hostname as a username and port as password ex. localhost/9500