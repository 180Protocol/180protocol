import auth from "./auth/reducer";
import provider from "./provider/reducer";
import consumer from "./consumer/reducer";
import {combineReducers} from "./../utils/helpers"

const reducers = combineReducers({
    auth,
    provider,
    consumer
});

export default reducers;
