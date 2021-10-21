export const login = (state, payload) => {
    const {user, history} = payload;

    localStorage.setItem('user', JSON.stringify(user));
    history.push("/app/dashboard");
    window.location.reload();
    return {...state, user: user};
};

export const logout = (state, payload) => {
    const {history} = payload;

    localStorage.removeItem('user');
    history.push("/user/login");
    // window.location.reload();
    return {...state}
};