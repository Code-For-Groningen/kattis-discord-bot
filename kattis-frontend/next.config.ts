import { NextConfig } from 'next';

const nextConfig: NextConfig = {
  output: "export",
  basePath: process.env.NODE_ENV === "production" ? "/kattis-leaderboard" : "",
  trailingSlash: true,
  skipTrailingSlashRedirect: true,
  rewrites: async () => {
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:8080/:path*",
      },
    ];
  }
};

export default nextConfig;
