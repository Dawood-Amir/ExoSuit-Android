package com.exosuit.exoappwithridgeregression

class NavGraph {
    sealed class Screen(val route: String) {
        object EmgHome : Screen("emg_home")
        object GuidedRecording : Screen("guided_recording")
    }
}