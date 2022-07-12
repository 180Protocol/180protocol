export async function upload(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        body: payload,
    };

    try {
        let flow = payload.storageType === 'filecoin' ? '180 Protocol Estuary Storage/EstauryStorageProviderAggregationInputFlow' : '180 Protocol Broker Flows/ProviderAggregationInputFlow';
        let response = await fetch(`${apiUrl}/node/${flow}`, requestOptions);
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
        method: 'GET'
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

export async function fetchDecryptedRewardsData(dispatch, apiUrl, payload, dateCreated) {
    const requestOptions = {
        method: 'POST',
        body: JSON.stringify(payload)
    };

    try {
        let response = await fetch(`${apiUrl}/node/180 Protocol Broker Flows/ProviderRewardOutputRetrievalFlow?wait=1`, requestOptions);
        let data = await response.json();
        let value = data.result.value ? data.result.value.split("\n") : [];
        let result = [];
        for (let i = 0; i < value.length; i++) {
            let parsedData = JSON.parse(value[i]);
            parsedData.flowId = data.flowRunId;
            parsedData.coApp = 'DMS';
            parsedData.date = dateCreated;
            result.push(parsedData);
        }

        if (result) {
            dispatch({type: 'FETCH_DECRYPTED_REWARDS_DATA_SUCCESS', payload: result});
            return result;
        }

        dispatch({type: 'FETCH_DECRYPTED_REWARDS_DATA_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'FETCH_DECRYPTED_REWARDS_DATA_ERROR', error: error});
        console.log(error);
    }
}

export async function updateDecentralizedStorageEncryptionKey(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        body: JSON.stringify(payload)
    };

    try {
        let response = await fetch(`${apiUrl}/node/180 Protocol Estuary Storage/DecentralizedStorageEncryptionKeyUpdateFlow`, requestOptions);
        let data = await response.json();

        if (data) {
            dispatch({type: 'UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'UPDATE_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR', error: error});
        console.log(error);
    }
}

export async function retrievalDecentralizedStorageEncryptionKey(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        body: JSON.stringify(payload)
    };

    try {
        let response = await fetch(`${apiUrl}/node/180 Protocol Estuary Storage/DecentralizedStorageEncryptionKeyRetrievalFlow?wait=1`, requestOptions);
        let data = await response.json();

        if (data) {
            dispatch({type: 'RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'RETRIEVAL_DECENTRALIZED_STORAGE_ENCRYPTION_KEY_ERROR', error: error});
        console.log(error);
    }
}