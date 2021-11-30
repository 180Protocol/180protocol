import {
    API_URL,
    DECRYPTED_REWARDS_DATA_RESPONSE,
    ENCRYPTED_REWARDS_DATA_RESPONSE
} from "../../utils/constants";

export async function upload(dispatch, payload) {
    const requestOptions = {
        method: 'POST',
        body: payload,
    };

    try {
        let response = await fetch(`${API_URL}/uploadNodeAttachment`, requestOptions);
        let data = await response.json();

        if (payload) {
            dispatch({type: 'UPLOAD_SUCCESS', payload: payload});
            return payload;
        }

        dispatch({type: 'UPLOAD_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'UPLOAD_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchEncryptedRewardsData(dispatch, payload) {
    // const requestOptions = {
    //     method: 'GET',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // };

    // try {
    // let response = await fetch(`${API_URL}/uploadNodeAttachment`, requestOptions);
    // let data = await response.json();

    // if (data) {
    dispatch({type: 'FETCH_REWARDS_SUCCESS', payload: ENCRYPTED_REWARDS_DATA_RESPONSE});
    return ENCRYPTED_REWARDS_DATA_RESPONSE;
    // }

    // dispatch({type: 'FETCH_REWARDS_ERROR', error: 'Error'});
    // return;
    // } catch (error) {
    //     dispatch({type: 'FETCH_REWARDS_ERROR', error: error});
    //     console.log(error);
    // }
}

export async function fetchDecryptedRewardsData(dispatch, payload) {
    // const requestOptions = {
    //     method: 'GET',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // };

    // try {
    // let response = await fetch(`${API_URL}/uploadNodeAttachment`, requestOptions);
    // let data = await response.json();

    // if (data) {
    dispatch({type: 'FETCH_REWARD_DETAIL_SUCCESS', payload: DECRYPTED_REWARDS_DATA_RESPONSE});
    return DECRYPTED_REWARDS_DATA_RESPONSE;
    // }

    // dispatch({type: 'FETCH_REWARD_DETAIL_ERROR', error: 'Error'});
    // return;
    // } catch (error) {
    //     dispatch({type: 'FETCH_REWARD_DETAIL_ERROR', error: error});
    //     console.log(error);
    // }
}