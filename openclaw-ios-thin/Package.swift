// swift-tools-version:5.7
import PackageDescription

let package = Package(
    name: "OpenClawThin",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "OpenClawThin",
            targets: ["OpenClawThin"]
        ),
    ],
    dependencies: [
        // Keine externen Dependencies - wir nutzen nur Apple Frameworks
    ],
    targets: [
        .target(
            name: "OpenClawThin",
            dependencies: [],
            path: "OpenClawThin",
            exclude: ["Info.plist"]
        ),
    ]
)
