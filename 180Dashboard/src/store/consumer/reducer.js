import {
    AGGREGATION_REQUEST,
    AGGREGATION_REQUEST_ERROR,
    AGGREGATION_REQUEST_SUCCESS,
    FETCH_DECRYPTED_DATA_OUTPUT,
    FETCH_DECRYPTED_DATA_OUTPUT_ERROR,
    FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS,
    FETCH_ENCRYPTED_DATA_OUTPUT,
    FETCH_ENCRYPTED_DATA_OUTPUT_ERROR,
    FETCH_ENCRYPTED_DATA_OUTPUT_SUCCESS,
    UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY,
    UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_SUCCESS,
    UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR
} from "../actions";

export const initialState = {
    updateDecentralizedStorageEncryptionKey: {},
    aggregationRequest: {},
    encryptedDataOutput: [],
    decryptedDataOutput: [],
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
        case AGGREGATION_REQUEST:
            return {...state, loading: true};
        case AGGREGATION_REQUEST_SUCCESS:
            return {...state, loading: false, aggregationRequest: action.payload};
        case AGGREGATION_REQUEST_ERROR:
            return {...state, loading: false};
        case FETCH_ENCRYPTED_DATA_OUTPUT:
            return {...state, loading: true};
        case FETCH_ENCRYPTED_DATA_OUTPUT_SUCCESS:
            return {...state, loading: false, encryptedDataOutput: action.payload};
        case FETCH_ENCRYPTED_DATA_OUTPUT_ERROR:
            return {...state, loading: false};
        case FETCH_DECRYPTED_DATA_OUTPUT:
            return {...state, loading: true};
        case FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS:
            return {...state, loading: false, decryptedDataOutput: action.payload};
        case FETCH_DECRYPTED_DATA_OUTPUT_ERROR:
            return {...state, loading: false};
        default:
            return state;
    }
};