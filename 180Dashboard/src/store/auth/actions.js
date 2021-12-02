import {getYamlInfo} from "../../utils/helpers";

export async function login(dispatch, payload) {
    try {
        dispatch({type: 'LOGIN'});
        let info = await getYamlInfo();
        let nodeInfo = Object.values(info.nodes).find((item) => {
            return item.username === payload.username && item.password === payload.password ? item : null
        });

        if (nodeInfo) {
            delete nodeInfo.password;
            delete nodeInfo.port;
            dispatch({type: 'LOGIN_SUCCESS', payload: nodeInfo});
            localStorage.setItem('user', JSON.stringify(nodeInfo));
            localStorage.setItem('rewards', JSON.stringify(info.rewards));
            return nodeInfo;
        } else {
            dispatch({type: 'LOGIN_ERROR', error: 'Error'});
            return null;
        }
    } catch (error) {
        dispatch({type: 'LOGIN_ERROR', error: error});
        console.log(error);
    }
}

export async function logout(dispatch) {
    dispatch({type: 'LOGOUT'});
    localStorage.removeItem('user');
}