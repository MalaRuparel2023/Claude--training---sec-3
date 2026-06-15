# Testing Notes

Classes that are NOT unit-testable on the JVM and are deferred to instrumented (androidTest) coverage, with the reason:

- `data.repository.JobRepositoryImpl` — constructed with a concrete `FirebaseFirestore` and a Room `JobDao`; its streams wrap Firestore snapshot listeners (`callbackFlow` + `addSnapshotListener`) and Room queries, and writes use `System.currentTimeMillis()`. None of these run on a plain JVM; requires an instrumented test with a Firestore emulator and an in-memory Room database.
- `data.repository.HealthRepositoryImpl` — depends directly on `FirebaseFirestore`; every method builds `CollectionReference`/`DocumentReference` queries and awaits real Firestore tasks. Needs the Firestore emulator under an instrumented test.
- `data.repository.YogaRepositoryImpl` — depends directly on `FirebaseFirestore`; observe/seed logic is Firestore snapshot listeners and document writes. Needs the Firestore emulator under an instrumented test.
