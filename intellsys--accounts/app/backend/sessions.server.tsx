import {getAccountsPostgresDatabaseManager} from "~/backend/users.server";
import type {Iso8601DateTime, Uuid} from "~/common--type-definitions/typeDefinitions";
import {getSingletonValue} from "~/global-common-typescript/utilities/utilities";

export type Session = {
    sessionId: Uuid;
    userId: Uuid;
    sessionCreatedAt?: Iso8601DateTime;
    sessionExpiresAt?: Iso8601DateTime;
    sessionLastUsed?: Iso8601DateTime;
};

export async function createSession(session: Session): Promise<void | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await postgresDatabaseManager.execute(
        `
            INSERT INTO
                sessions
            VALUES (
                $1,
                $2,
                $3,
                $4,
                $5,
                false
            )
        `,
        [session.sessionId, session.userId, session.sessionCreatedAt, session.sessionExpiresAt, session.sessionLastUsed],
    );

    if (result instanceof Error) {
        return result;
    }
}

export async function getEmailFromSessionId(sessionId: string): Promise<string | null | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await postgresDatabaseManager.execute(
        `
            SELECT
                email
            FROM
                users,
                sessions
            WHERE
                sessions.session_id = $1 AND
                sessions.user_id = users.id
        `,
        [sessionId],
    );

    if (result instanceof Error) {
        return result;
    }

    if (result.rowCount === 0) {
        return null;
    }

    return result.rows[0].email;
}

export async function getExistingSessionId(email: string): Promise<Uuid | null | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await postgresDatabaseManager.execute(
        `
            SELECT
                users.id,
                sessions.session_id
            FROM
                users,
                sessions
            WHERE
                users.email = $1 AND
                sessions.session_expires_at > CURRENT_TIMESTAMP AND
                is_deleted = false
        `,
        [email],
    );

    if (result instanceof Error) {
        return result;
    }

    if (result.rowCount === 0) {
        return null;
    }

    return result.rows[0].session_id;
}

export async function checkIfSessionIsValid(sessionId: Uuid): Promise<string | null | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await postgresDatabaseManager.execute(
        `
            SELECT
                user_id
            FROM
                sessions
            WHERE
                session_id = $1 AND
                session_expires_at > CURRENT_TIMESTAMP AND
                is_deleted = false
        `,
        [sessionId],
    );

    if (result instanceof Error) {
        return result;
    }

    if (result.rowCount === 0) {
        return null;
    }

    return result.rows[0].user_id;
}

export async function markSessionAsDeleted(sessionId: Uuid): Promise<void | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await postgresDatabaseManager.execute(
        `
            UPDATE
                sessions
            SET
                is_deleted = true
            WHERE
                session_id = $1
        `,
        [sessionId],
    );

    if (result instanceof Error) {
        return result;
    }
}
