import axios from 'axios'

// creates as blank account as possible
export function createDefault() {
    return {name: "", sex: "Male", birthday:"0001-01-01", password: "", login:""}
}

// validates JSON description of an account with accordance to some constraints
// login: contains only letters, digits and underscores
// birthday: a date in YYYY-MM-DD format
// sex: either Male or Female
// throws an Error instance if the account is invalid
export function validate(account) {
    if(typeof(account.birthday) != "string" || isNaN(Date.parse(account.birthday))) {
        throw new Error("Invalid birthday:"+account.birthday"; Should be in format YYYY-MM-DD");
    }
    account.birthday = new Date(account.birthday).toISOString().substring(0, 10)

    if (typeof (account.name) != "string")
        throw new Error("Invalid name")
    if(typeof (account.password) != "string")
        throw new Error("Invalid password")
    if(typeof (account.login) != "string" || /[^a-zA-Z0-9_]/.test(account.login))
        throw new Error("Invalid login: "+account.login+"; Should contain only letters, digits, and underscores.")
    if(!("Male" === account.sex || "Female" === account.sex)) {
        throw new Error("Invalid sex: "+account.sex+"; Acceptable values are Male and Female")
    }

}

// post request
export async function post(account, apiAddress) {
    validate(account)

    await axios({
        method:'post',
        url: apiAddress,
        data: account
    })
}

// get request
export async function get(login, apiAddress) {
    let account = (await axios({method:'get', url: apiAddress+login})).data
    return account
}

// put request
export async function update(account, login, apiAddress) {
    validate(account)

    await axios({
        method:'put',
        url: apiAddress+login,
        data: account
    })
}

// delete request
export async function remove(login, apiAddress) {
    await axios({
        method:'delete',
        url: apiAddress+login
    })
}

