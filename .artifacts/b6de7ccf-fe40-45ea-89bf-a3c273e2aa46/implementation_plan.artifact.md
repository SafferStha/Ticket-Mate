# Fix Android Resource Linking Failed (Missing stableIds.txt)

The error indicates that the `processDebugResources` task is failing because AAPT2 expects a `stableIds.txt` file which is not found in the intermediates directory. This is typically related to **Resource ID Stabilization**, a feature in newer Android Gradle Plugin (AGP) versions (8.3+) that is more strictly enforced in AGP 9.0.

## User Review Required

> [!IMPORTANT]
> The error often occurs due to corrupted build artifacts or a mismatch in how AGP 9.0 handles resource optimizations. We will first try to clean the project and, if the error persists, disable the explicit resource optimization flag.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/A S U S/Documents/GitHub/Ticket-Mate/gradle.properties)
- We will add `android.enableResourceOptimizations=false` to ensure AGP does not attempt to use stable IDs if they are not correctly generated, which avoids passing the `--stable-ids` flag to AAPT2.
- We will also ensure `android.nonFinalResIds=true` is set, as this is the modern default and recommended for Compose projects.

## Verification Plan

### Automated Tests
- Run `./gradlew clean :app:processDebugResources` to verify that resources can be linked successfully.
- Run `./gradlew :app:assembleDebug` to ensure a full build succeeds.

### Manual Verification
- Verify that the `build/intermediates/stable_resource_ids_file` directory is either correctly populated or no longer causing build failures.
