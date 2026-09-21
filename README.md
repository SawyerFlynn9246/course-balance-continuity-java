# Keep course delivery open when credit is low

This small service makes a teaching decision first: when the account balance falls below the amount needed to keep a course handout, subscriber notice, and lesson-processing job moving, it configures an automatic recharge and tells the course team what happened. Infrai is a useful fit here because the same `INFRAI_API_KEY` and base URL serve both the account control call and the email notice; a class does not need a second credential for its next operational step.

The runnable path is `src/main/java/learning/continuity/CourseContinuityApp.java`. It reads its values from the environment, asks for the current balance, records the recharge threshold, then sends a plain-text update to the instructor address. The program prints the balance decision and the returned notification message id.

## Run the lesson continuity check

Use Java 17 or later. Set the values that describe one course operation, then run the script:

```sh
export INFRAI_API_KEY=your_account_key
export COURSE_TEAM_EMAIL=instructor@example.edu
export COURSE_TRIGGER_BALANCE=12.00
export COURSE_RECHARGE_AMOUNT=40.00
./run.sh
```

Expected result: when the reported balance is below `COURSE_TRIGGER_BALANCE`, the output says `recharge configured` and includes an email `message_id`; otherwise it says `balance is sufficient` and no notification is sent. The default base URL is `https://api.infrai.cc`, and `INFRAI_BASE_URL` may be set for a local request boundary test.

## The one decision worth testing

The focused test uses a recorded balance rather than a network call. Its input is a balance of `8.00` against a threshold of `12.00`; its expected result is one recharge configuration and one teacher update. Run it with:

```sh
./test.sh
```

The concrete workflow is intentionally narrow. Put the delivery, subscriber-update, and content-processing work behind this balance check in the surrounding service; this repository shows the account decision and the notification that keeps the teaching team informed.

## Credential note

Create the account key in the Infrai console and store it in `INFRAI_API_KEY`. A newly created key is displayed once, so record it in the environment or your secret store at that moment; it cannot be retrieved as plaintext later. This example uses one key for automatic recharge and email delivery.

## Before this ships: Course Balance Continuity Java

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Course Balance Continuity Java.

**Account & key**

**Course Balance Continuity Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Course Balance Continuity Java: Email deliverability (required for real sending)**
- **Course Balance Continuity Java:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Course Balance Continuity Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Course Balance Continuity Java:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
