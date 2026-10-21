FROM node:20-bookworm AS builder
WORKDIR /app
COPY backend/package*.json ./
RUN npm ci
COPY backend/ ./
RUN npm run build

FROM node:20-bookworm AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=5000
COPY backend/package*.json ./
RUN npm ci --only=production
COPY --from=builder /app/dist ./dist
EXPOSE 5000
CMD ["node", "dist/index.js"]
