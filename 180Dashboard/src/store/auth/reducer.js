import {login, logout} from "./service";

const INIT_STATE = {
    user: localStorage.getItem("user")
        ? JSON.parse(localStorage.getItem("user"))
        : null,
    loading: false
};

export default (state = INIT_STATE, action) => {
    switch (action.type) {
        case 'LOGIN':
            return login(state, action.payload);
        case 'LOGOUT':
            return logout(state, action.payload);
        default:
            return state;
    }
};