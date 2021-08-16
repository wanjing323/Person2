package my.edu.utar.person.application

data class UserModel(
    val email: String,
    val loginType: String,
    val phone: String,
    val uid: String,
    val username: String
){
    constructor(): this("", "", "", "", "")

    override fun toString(): String {
        return "$email $loginType $phone $uid $username"
    }
}
