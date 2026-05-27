### Summary
This PR introduces significant performance optimizations and code quality improvements, specifically targeting scrolling smoothness on mid-range devices and reactive data consistency. A key focus was reducing the overhead of active downloads on UI performance.

### Key Improvements
1. **Scrolling Performance (Metadata Hoisting)**:
   - Previously, list and grid items (songs, albums, artists) were performing individual database lookups or observing `DataStore` state directly within their Composables. This caused high recomposition overhead and database contention during rapid scrolling.
   - **Solution**: Introduced an `ItemMetadata` data class. Metadata (liked status, in-library status, download state) is now aggregated at the ViewModel level and passed down to items as pre-calculated states. This removes redundant IO operations from the UI thread during scrolling.

2. **Download Update Optimization (Preventing Redundant Mapping)**:
   - **Problem**: When a download was active, the `allItemsMetadata` flow and several download-related lists would re-process the entire library for every single download progress/state emission. In large libraries, this caused constant CPU spikes and list stutters just to update a single download icon.
   - **Solution**: Refactored the metadata aggregation logic to decouple static library metadata (isLiked, isInLibrary) from volatile download states. Download updates are now merged into a cached metadata map, avoiding full database re-scans. Similarly, download-filtered lists in `LibraryViewModels` and `AutoPlaylistViewModel` were optimized using the `combine` operator to avoid re-triggering expensive database queries on every download update.

3. **Reactive Architecture Refactor**:
   - Refactored `HomeViewModel` to use the `combine` operator for `allLocalItems` and `allYtItems`.
   - This ensures that UI components always see a consistent, single source of truth and eliminates potential race conditions during the initial page load.

4. **InnerTube Metadata Extraction**:
   - Fixed a "TODO" in `YouTube.kt` to correctly extract explicit badges for albums from the InnerTube responsive header badges.

5. **Codebase Cleanup**:
   - Removed accidental empty files (`div`, `div.blyrics--active`) from the project root.

### Technical Details
- Added `ItemMetadata` class in `com.arturo254.opentune.models`.
- Updated `HomeViewModel` to use a split `libraryMetadata` / `allItemsMetadata` strategy to efficiently merge download updates.
- Refactored `LibrarySongsViewModel`, `LibraryAlbumsViewModel`, and `AutoPlaylistViewModel` to optimize download-filtered flows using `combine`.
- Refactored `QuickPicksSection`, `SpeedDialSection`, `KeepListeningSection`, and other home screen components to accept and propagate metadata.
- Updated `SongListItem`, `SongGridItem`, `YouTubeGridItem`, etc., to prioritize passed-in metadata over local database lookups.
