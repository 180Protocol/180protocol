# The Node version that we'll be running for our version of React.
# You may have to search the Node directory for a version that fits
# the version of React you're using.
FROM node:16-alpine
# Create a work directory and copy over our dependency manifest files.
RUN mkdir /app
# Giving access to 'node' user for work directory
RUN chown node:node /app
USER node
WORKDIR /app
COPY --chown=node:node 180Dashboard/src /app/src
COPY --chown=node:node ["180Dashboard/package.json", "180Dashboard/package-lock.json*", "./"]
RUN CI=true
RUN npm install --silent
# Expose PORT 3000 on our virtual machine so we can run our server
EXPOSE 3000