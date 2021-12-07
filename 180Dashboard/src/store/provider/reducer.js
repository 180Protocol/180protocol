import {
    UPLOAD,
    UPLOAD_SUCCESS,
    UPLOAD_ERROR,
    FETCH_ENCRYPTED_REWARDS_DATA,
    FETCH_ENCRYPTED_REWARDS_DATA_SUCCESS,
    FETCH_ENCRYPTED_REWARDS_DATA_ERROR,
    FETCH_DECRYPTED_REWARDS_DATA,
    FETCH_DECRYPTED_REWARDS_DATA_SUCCESS,
    FETCH_DECRYPTED_REWARDS_DATA_ERROR
} from "../actions";

export const initialState = {
    uploadData: {},
    encryptedRewardsData: [],
    decryptedRewardsData: {},
    loading: false
};

export default (state = initialState, action) => {
    switch (action.type) {
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