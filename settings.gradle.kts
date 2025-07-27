rootProject.name = "Veloctopus Rising"

// Core API and Communication Hub
include("api")
include("core")

// Only include modules that have actual build.gradle.kts files
include("modules:velocity-module")
include("modules:common-module")
include("modules:discord-integration")

// Reference projects for study and patterns (temporarily disabled due to build issues)
// include("references:VeloctopusProject")
// include("references:Spicord")
// include("references:HuskChat")
// include("references:Ban-Announcer")
// include("references:PAPIProxyBridge")
// include("references:Velocitab")
// include("references:VelemonAId")
// include("references:discord-ai-bot")
// include("references:SignedVelocity")
// include("references:ChatRegulator")
// include("references:EpicGuard")
// include("references:VLobby")
// include("references:VPacketEvents")
// include("references:KickRedirect")
