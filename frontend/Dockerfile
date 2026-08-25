FROM node:18-alpine AS build
WORKDIR /app

# Vite incrusta las env vars VITE_* en el bundle al momento del build, no en
# runtime, asi que hay que pasarla como build arg:
# docker build --build-arg VITE_API_URL=https://api.ejemplo.com .
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build

FROM nginx:alpine AS production
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
