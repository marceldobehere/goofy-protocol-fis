import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    output: "export",
    // TODO: SET // basePath: "/goofy-protocol-fis-fe",
    images: {
        unoptimized: true
    }
};

export default nextConfig;
