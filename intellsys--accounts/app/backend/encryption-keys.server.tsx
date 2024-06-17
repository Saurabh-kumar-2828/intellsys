import {getAccountsPostgresDatabaseManager} from "~/backend/users.server";
import {generateRandomString} from "~/utilities/utilities";
import Cryptr from "cryptr";
import {getNonEmptyStringFromUnknown} from "~/global-common-typescript/utilities/typeValidationUtilities";
import {getRequiredEnvironmentVariable} from "~/common-remix--utilities/utilities.server";

export async function getEncryptionKeyForHost(host: string): Promise<string | null | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const result = await postgresDatabaseManager.execute(
        `
            SELECT
                encryption_key
            FROM
                encryption_keys
            WHERE
                host = $1
        `,
        [host],
    );

    if (result instanceof Error) {
        return result;
    }

    if (result.rowCount === 0) {
        return null;
    }

    return result.rows[0].encryption_key;
}

export async function createEncryptionKeyForHost(host: string): Promise<string | Error> {
    const postgresDatabaseManager = await getAccountsPostgresDatabaseManager();
    if (postgresDatabaseManager instanceof Error) {
        return postgresDatabaseManager;
    }

    const encryptionKey = generateRandomString(32);
    const cryptrCommon = new Cryptr(getNonEmptyStringFromUnknown(getRequiredEnvironmentVariable("ENCRYPTION_KEYS_ENCRYPTION_KEY")));

    const encryptedEncryptionKey = cryptrCommon.encrypt(encryptionKey);

    const result = await postgresDatabaseManager.execute(
        `
            INSERT INTO
                encryption_keys
            VALUES (
                $1,
                $2
            )
        `,
        [host, encryptedEncryptionKey],
    );

    if (result instanceof Error) {
        return result;
    }

    return encryptedEncryptionKey;
}
