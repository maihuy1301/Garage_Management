FROM node:20-alpine

WORKDIR /app

# Copy dependency manifests
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm install

# Copy application source
COPY . .

# Expose Vite dev server port
EXPOSE 3001

# Start Vite development server (vite.config.ts is already configured with host: true, port: 3001)
CMD ["npm", "run", "dev"]
