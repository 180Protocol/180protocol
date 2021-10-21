import React, {createContext, useReducer} from 'react';
import reducers from './reducers';

export const Store = createContext({});

export const StoreProvider = ({children}) => {
    const [state, dispatch] = useReducer(reducers, {});

    return <Store.Provider value={{state, dispatch}}>{children}</Store.Provider>;
};