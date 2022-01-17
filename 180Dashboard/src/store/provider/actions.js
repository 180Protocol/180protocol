export async function upload(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        body: payload,
    };

    try {
        let response = await fetch(`${apiUrl}/node/uploadNodeAttachment`, requestOptions);
        let data = await response.json();

        if (data) {
            dispatch({type: 'UPLOAD_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'UPLOAD_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'UPLOAD_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchEncryptedRewardsData(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    };

    let userInfo = JSON.parse(localStorage.getItem('user'));

    try {
        let response = await fetch(`${apiUrl}/node/180Protocol Broker Contracts/RewardsState/query?participant=${encodeURIComponent(userInfo.name)}`, requestOptions);
        let data = await response.json();
        if (data) {
            dispatch({type: 'FETCH_ENCRYPTED_REWARDS_DATA_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'FETCH_ENCRYPTED_REWARDS_DATA_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'FETCH_ENCRYPTED_REWARDS_DATA_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchDecryptedRewardsData(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    };

    try {
        let response = await fetch(`${apiUrl}/node/180 Protocol Broker Flows/ProviderRewardOutputRetrievalFlow?wait=3`, requestOptions);
        let data = await response.json();
        let value = JSON.parse(data.result.value);
        let sum = 0;
        for (let i = 0; i < value.length; i++) {
            sum += parseFloat(value[i].rewards);
            value[i].rewardsBalance = sum;
        }

        if (data) {
            dispatch({type: 'FETCH_DECRYPTED_REWARDS_DATA_SUCCESS', payload: value});
            return data;
        }

        dispatch({type: 'FETCH_DECRYPTED_REWARDS_DATA_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'FETCH_DECRYPTED_REWARDS_DATA_ERROR', error: error});
        console.log(error);
    }
}