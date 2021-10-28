import {
    LOGIN,
    LOGIN_SUCCESS,
    LOGIN_ERROR,
    LOGOUT
} from "../actions";

export const initialState = {
    user: localStorage.getItem("user")
        ? JSON.parse(localStorage.getItem("user"))
        : null,
    loading: false
};

export default (state = initialState, action) => {
    switch (action.type) {
        case LOGIN:
            return {...state, loading: true};
        case LOGIN_SUCCESS:
            return {...state, loading: false, user: action.payload};
        case LOGIN_ERROR:
            return {...state, loading: false};
        case LOGOUT:
            return {...state, user: null};
        default:
            return state;
    }
};