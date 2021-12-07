import {
    AGGREGATION_REQUEST,
    AGGREGATION_REQUEST_ERROR,
    AGGREGATION_REQUEST_SUCCESS,
    FETCH_DECRYPTED_DATA_OUTPUT,
    FETCH_DECRYPTED_DATA_OUTPUT_ERROR,
    FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS,
    FETCH_ENCRYPTED_DATA_OUTPUT,
    FETCH_ENCRYPTED_DATA_OUTPUT_ERROR,
    FETCH_ENCRYPTED_DATA_OUTPUT_SUCCESS
} from "../actions";

export const initialState = {
    aggregationRequest: {},
    encryptedDataOutput: [],
    decryptedDataOutput: [],
    loading: false
};

export default (state = initialState, action) => {
    switch (action.type) {
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