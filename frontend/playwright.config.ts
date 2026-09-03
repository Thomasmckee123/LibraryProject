import { defineConfig, devices } from "@playwright/test";

/**
 * Starts both halves of the app and drives the real UI.
 *
 * The backend uses an in-memory H2 that is rebuilt on every boot, so each run
 * starts from the same seeded catalogue. That matters: these tests buy things,
 * which permanently changes stock. `reuseExistingServer` is off in CI so a run
 * never inherits another run's depleted shelves.
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false, // one shared cart and one stock pool - see above
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [["github"], ["list"]] : [["list"]],

  use: {
    baseURL: "http://localhost:5173",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },

  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],

  webServer: [
    {
      command: "mvn -B -q spring-boot:run",
      cwd: "../backend",
      url: "http://localhost:8080/api/books",
      reuseExistingServer: !process.env.CI,
      timeout: 120_000,
    },
    {
      command: "npm run dev",
      url: "http://localhost:5173",
      reuseExistingServer: !process.env.CI,
      timeout: 60_000,
    },
  ],
});
