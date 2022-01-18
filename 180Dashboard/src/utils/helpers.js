import userInfo from "./../userInfo.yml";
import jsYaml from "yamljs";

export const combineReducers = (slices) => (state, action) =>
    Object.keys(slices).reduce( // use for..in loop, if you prefer it
        (acc, prop) => ({
            ...acc,
            [prop]: slices[prop](acc[prop], action),
        }),
        state
    );

export const average = (arr, field) => {
    return (arr.reduce(function (sum, item) {
        return sum + parseFloat(item[field]);
    }, 0) / arr.length).toFixed(1);
}

export const sum = (arr, field) => {
    return (arr.reduce(function (sum, item) {
        return sum + parseFloat(item[field]);
    }, 0)).toFixed(1);
}

export const ucWords = (str) => {
    const result = str.replace(/([A-Z])/g, " $1");
    return result.charAt(0).toUpperCase() + result.slice(1);
}

export const getYamlInfo = async () => {
    let response = await fetch(userInfo);
    let data = await response.text();
    return jsYaml.parse(data);
}