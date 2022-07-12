import {
    UPLOAD,
    UPLOAD_SUCCESS,
    UPLOAD_ERROR,
    FETCH_ENCRYPTED_REWARDS_DATA,
    FETCH_ENCRYPTED_REWARDS_DATA_SUCCESS,
    FETCH_ENCRYPTED_REWARDS_DATA_ERROR,
    FETCH_DECRYPTED_REWARDS_DATA,
    FETCH_DECRYPTED_REWARDS_DATA_SUCCESS,
    FETCH_DECRYPTED_REWARDS_DATA_ERROR,
    UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY,
    UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_SUCCESS,
    UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR,
    RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY,
    RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_SUCCESS,
    RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR
} from "../actions";

export const initialState = {
    updateDecentralizedStorageEncryptionKey: {},
    retrievalDecentralizedStorageEncryptionKey: {},
    uploadData: {},
    encryptedRewardsData: [],
    decryptedRewardsData: {},
    loading: false
};

export default (state = initialState, action) => {
    switch (action.type) {
        case UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY:
            return {...state, loading: true};
        case UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_SUCCESS:
            return {...state, loading: false, updateDecentralizedStorageEncryptionKey: action.payload};
        case UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR:
            return {...state, loading: false};
        case RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY:
            return {...state, loading: true};
        case RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_SUCCESS:
            return {...state, loading: false, retrievalDecentralizedStorageEncryptionKey: action.payload};
        case RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR:
            return {...state, loading: false};
        case UPLOAD:
            return {...state, loading: true};
        case UPLOAD_SUCCESS:
            return {...state, loading: false, uploadData: action.payload};
        case UPLOAD_ERROR:
            return {...state, loading: false};
        case FETCH_ENCRYPTED_REWARDS_DATA:
            return {...state, loading: true};
        case FETCH_ENCRYPTED_REWARDS_DATA_SUCCESS:
            return {...state, loading: false, encryptedRewardsData: action.payload};
        case FETCH_ENCRYPTED_REWARDS_DATA_ERROR:
            return {...state, loading: false};
        case FETCH_DECRYPTED_REWARDS_DATA:
            return {...state, loading: true};
        case FETCH_DECRYPTED_REWARDS_DATA_SUCCESS:
            return {...state, loading: false, decryptedRewardsData: action.payload};
        case FETCH_DECRYPTED_REWARDS_DATA_ERROR:
            return {...state, loading: false};
        default:
            return state;
    }
};