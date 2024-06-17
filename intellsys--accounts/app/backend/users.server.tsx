import {getPostgresDatabaseManager} from "~/common--database-manager--postgres/postgresDatabaseManager.server";
import {Iso8601DateTime, Uuid} from "~/common--type-definitions/typeDefinitions";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";
import {getUuidFromUnknown} from "~/global-common-typescript/utilities/typeValidationUtilities";
import {generateUuid} from "~/global-common-typescript/utilities/utilities";

/**
 * @userMetaData is a stringfied JSON
 * @googleAccountInformation is a stringified JSON
 */
export type User = {
    id: Uuid;
    email: string;
    phoneNumber?: string;
    name?: string;
    passwordHashed?: string;
    salt?: string;
    isEmailValidated?: boolean;
    userMetaData?: string; // Stringified JSON
    isLocked?: boolean;
    isBanned?: boolean;
    googleAccountInformation?: string; // Stringified JSON
};

export async function getAccountsPostgresDatabaseManager() {
    const postgresDatabaseManager = await getPostgresDatabaseManager(getUuidFromUnknown(getRequiredEnvironmentVariable("DATABASE_CREDENTIAL_ID")));
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    return postgresDatabaseManager;
}

export async function checkIfUserExists(email: string): Promise<Uuid | null | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await postgresDatabaseManager.execute(
        `
            SELECT
                *
            FROM
                users
            WHERE
                email = $1
        `,
        [email],
    );

    if (result instanceof Error) {
        return result;
    }

    if (result.rowCount === 0) {
        return null;
    }

    return result.rows[0].id;
}

export async function insertUserInDatabase(user: User) {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const insertResult = await postgresDatabaseManager.execute(
        `
            INSERT INTO
                users
            VALUES (
                $1,
                $2,
                $3,
                $4,
                $5,
                $6,
                $7,
                $8,
                $9,
                $10,
                $11
            );
        `,
        [user.id, user.email, user.phoneNumber, user.name, user.passwordHashed, user.salt, user.isEmailValidated, user.userMetaData, user.isLocked, user.isBanned, user.googleAccountInformation],
    );

    if (insertResult instanceof Error) {
        return insertResult;
    }
}
