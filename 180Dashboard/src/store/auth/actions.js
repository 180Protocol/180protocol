export async function login(dispatch, payload) {
    try {
        dispatch({type: 'LOGIN'});

        if (payload && payload.username === process.env.REACT_APP_USERNAME && payload.password === process.env.REACT_APP_PASSWORD) {
            payload.role = process.env.REACT_APP_ROLE;
            dispatch({type: 'LOGIN_SUCCESS', payload: payload});
            localStorage.setItem('user', JSON.stringify(payload));
            return payload;
        }

        dispatch({type: 'LOGIN_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'LOGIN_ERROR', error: error});
        console.log(error);
    }
}

export async function logout(dispatch) {
    dispatch({type: 'LOGOUT'});
    localStorage.removeItem('user');
}