import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    output: "export",
    trailingSlash: true,
    // TODO: SET // basePath: "/goofy-protocol-fis-fe",
    images: {
        unoptimized: true
    }
};

export default nextConfig;
