const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-bugtaedi-8ee1c4d8-0e12-49dc-9ed3-3d1741b2e625";
const ALICE_UID = "alice_bugti_123";
const BOB_UID = "bob_bugti_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      databaseId: DATABASE_ID,
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write cloud_records", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const col = unauthDb.collection("users").doc(ALICE_UID).collection("cloud_records");
  await assertFails(col.get());
  await assertFails(
    col.doc("rec_1").set({
      userId: ALICE_UID,
      title: "Unauthorized Pack",
      category: "Preset",
      payload: "Contrast +20",
      appTag: "Bugti Editz",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated owner: Kotlin-mirrored create, list, partial update, and delete succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("cloud_records");
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    col.doc("rec_1").set({
      userId: ALICE_UID,
      title: "Bugti Editz 4K Preset",
      category: "Preset",
      payload: "Ultra HD 85% + Golden Hour",
      appTag: "Bugti Editz",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(col.get());

  await assertSucceeds(
    col.doc("rec_1").update({
      title: "Bugti Editz 4K Pro",
      payload: "AI UHD 92% + Tokyo Night",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(col.doc("rec_1").delete());
});

test("Rejects @DocumentId 'id' field, ghost fields, immutable mutation, and future timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("cloud_records");
  const nowPast = new Date(Date.now() - 5000);

  await assertFails(
    col.doc("rec_id_bad").set({
      id: "rec_id_bad",
      userId: ALICE_UID,
      title: "Bad ID",
      category: "Preset",
      payload: "Payload",
      appTag: "Bugti Editz",
      createdAt: nowPast,
      updatedAt: nowPast,
    })
  );

  await assertFails(
    col.doc("rec_ghost").set({
      userId: ALICE_UID,
      title: "Ghost",
      category: "Preset",
      payload: "Payload",
      appTag: "Bugti Editz",
      isAdmin: true,
      createdAt: nowPast,
      updatedAt: nowPast,
    })
  );

  await assertFails(
    col.doc("rec_future").set({
      userId: ALICE_UID,
      title: "Future",
      category: "Preset",
      payload: "Payload",
      appTag: "Bugti Editz",
      createdAt: new Date(Date.now() + 120000),
      updatedAt: nowPast,
    })
  );

  await assertSucceeds(
    col.doc("rec_imm").set({
      userId: ALICE_UID,
      title: "Immutable Test",
      category: "Asset",
      payload: "Data",
      appTag: "Bugti Editz",
      createdAt: nowPast,
      updatedAt: nowPast,
    })
  );

  await assertFails(
    col.doc("rec_imm").update({
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user isolation: Alice cannot read, list, update, or delete Bob's records", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore()
      .collection("users").doc(BOB_UID)
      .collection("cloud_records").doc("bob_rec")
      .set({
        userId: BOB_UID,
        title: "Bob Private",
        category: "User Data",
        payload: "Secret",
        appTag: "Bugti Editz",
        createdAt: new Date(Date.now() - 10000),
        updatedAt: new Date(Date.now() - 10000),
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobCol = aliceDb.collection("users").doc(BOB_UID).collection("cloud_records");

  await assertFails(bobCol.get());
  await assertFails(bobCol.doc("bob_rec").get());
  await assertFails(bobCol.doc("bob_rec").update({ title: "Hijacked" }));
  await assertFails(bobCol.doc("bob_rec").delete());
});
