# 1. Build React application
FROM node:24-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build

# 2. Serve static files
FROM nginx:alpine AS runtime

LABEL org.opencontainers.image.source="https://github.com/kho903/JihunPage" \
      org.opencontainers.image.description="JihunPage React frontend"

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder \
    /app/dist \
    /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]