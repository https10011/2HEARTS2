# Phase 17 — Media and rendering performance

Memories and Places load image previews sized for their display. The decoder reads dimensions first, samples the source before allocating its pixels, and limits concurrent decodes to two. A small byte-bounded cache reuses previews within the process. The app keeps original files at their saved resolution. Import now limits bytes during the copy and deletes incomplete output when a provider fails or returns more data than declared.

The Memories and Places feeds use one observable attachment query apiece. Memory detail fetches its ordered attachments with one joined query. These queries exclude removed records and media while preserving the existing Room schema and associations.

Home reads Yuki’s saved presence off the main thread and refreshes it when the route resumes. The companion room continues to use its existing bounded pose cache and pauses decorative activity when it is out of view.

## Verification

- A native bitmap test decoded a 4,096 × 3,072 source to 1,024 × 768. The preview’s pixel allocation is at most one-sixteenth of the source allocation. The same key returns the cached preview. The encoded source length is unchanged.
- In-memory Room checks verify attachment ordering and removal from both feeds when associated records or assets are deleted. A streaming test checks enforcement of the size limit without a trusted length.
- Reviewed the cached image and empty frame renders at 320 dp. [Compose review](evidence/compose-review.jpg)
- The full debug and release unit suites passed with 125 tests each, and the debug APK assembled. Launch timing, scrolling, memory pressure, and battery behavior on a physical device remain pending.
