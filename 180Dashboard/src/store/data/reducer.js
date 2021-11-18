import {
    UPLOAD,
    UPLOAD_SUCCESS,
    UPLOAD_ERROR
} from "../actions";

export const initialState = {
    uploadData: {},
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
        default:
            return state;
    }
};