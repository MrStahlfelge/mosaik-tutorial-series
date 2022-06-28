package com.example.ergomosaik.mosaikapp

import org.ergoplatform.mosaik.*
import org.ergoplatform.mosaik.model.MosaikApp
import org.ergoplatform.mosaik.model.ui.layout.Padding
import org.ergoplatform.mosaik.model.ui.text.LabelStyle
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class MosaikAppController {
    @GetMapping("/")
    fun getMainPage(): MosaikApp {
        return mosaikApp(
            "First Mosaik App", // app name shown in executors
            1 // the app version
        ) {
            // define the view here
            card {
                column(Padding.DEFAULT) {
                    label("Hello Ergo world!", LabelStyle.HEADLINE2)

                    box(Padding.HALF_DEFAULT)

                    button("Click me") {
                        onClickAction(showDialog("You clicked the button.", "myaction"))
                    }
                }
            }
        }
    }
}