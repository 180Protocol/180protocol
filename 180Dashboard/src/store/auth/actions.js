import {LOGIN, LOGOUT} from "../actions";

export const login = (user, history, dispatch) => {
    dispatch({
        type: LOGIN,
        payload: {user, history}
    })
};

export const logout = (history, dispatch) => {
    dispatch({
        type: LOGOUT,
        payload: {history}
    });
};