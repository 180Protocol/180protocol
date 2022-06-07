export async function createAggregationRequest(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        body: JSON.stringify(payload)
    };
    try {
        let flow = payload.storageType === 'filecoin' ? '180 Protocol Estuary Storage/EstuaryStorageConsumerAggregationFlow' : '180 Protocol Broker Flows/ConsumerAggregationFlow';
        let response = await fetch(`${apiUrl}/node/${flow}`, requestOptions);
        let data = await response.json();

        if (data) {
            dispatch({type: 'DATA_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'DATA_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'DATA_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchEncryptedDataOutput(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'GET'
    };

    let userInfo = JSON.parse(localStorage.getItem('user'));

    try {
        let response = await fetch(`${apiUrl}/node/180Protocol Broker Contracts/DataOutputState/query?participant=${encodeURIComponent(userInfo.name)}`, requestOptions);
        let data = await response.json();
        if (data) {
            dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchDecryptedDataOutput(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        body: JSON.stringify(payload)
    };

    try {
        let flow = payload.storageType === 'filecoin' ? '180 Protocol Estuary Storage/EstuaryStorageConsumerDataOutputRetrievalFlow' : '180 Protocol Broker Flows/ConsumerDataOutputRetrievalFlow';
        let response = await fetch(`${apiUrl}/node/${flow}?wait=1`, requestOptions);
        let data = await response.json();
        let value = data.result.value ? data.result.value.split("\n") : [];
        let result = [];
        for (let i = 0; i < value.length; i++) {
            let parsedData = JSON.parse(value[i]);
            for (const [key, value] of Object.entries(parsedData)) {
                if (value instanceof Object) {
                    let data = [];
                    for (const [k, v] of Object.entries(value.data)) {
                        data.push(k + ":" + v);
                    }
                    parsedData[key] = data.join(",");
                } else {
                    parsedData[key] = typeof value === "number" ? Intl.NumberFormat().format(value) : value;
                }
            }

            result.push(parsedData);
        }

        if (result) {
            dispatch({type: 'FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS', payload: result});
            return result;
        }

        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: error});
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