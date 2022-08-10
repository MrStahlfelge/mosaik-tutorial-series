package com.example.ergomosaik.mosaikapp

import org.ergoplatform.mosaik.*
import org.ergoplatform.mosaik.model.MosaikApp
import org.ergoplatform.mosaik.model.ui.ForegroundColor
import org.ergoplatform.mosaik.model.ui.layout.Padding
import org.ergoplatform.mosaik.model.ui.text.LabelStyle
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView

@RestController
@CrossOrigin
class MosaikAppController {
    @GetMapping("/")
    fun browserHintPage(): ModelAndView {
        return ModelAndView("nobrowser.html")
    }

    @GetMapping("/firstapp")
    fun getMainPage(): MosaikApp {
        return mosaikApp(
            "First Mosaik App", // app name shown in executors
            1 // the app version
        ) {
            // define the view here
            card {
                column(Padding.DEFAULT) {
                    label("Hello Ergo world!", LabelStyle.HEADLINE2) {
                        id = "titleLabel"
                    }

                    box(Padding.HALF_DEFAULT)

                    textInputField("inputId", "Enter your name") {
                        minValue = 3
                        maxValue = 10
                    }

                    box(Padding.HALF_DEFAULT)

                    button("Click me") {
                        onClickAction(backendRequest("enteredName"))
                    }
                }
            }
        }
    }

    @PostMapping("/enteredName")
    fun userEnteredName(@RequestBody values: Map<String, Any?>) =
        backendResponse(
            1,
            changeView(mosaikView {
                box {
                    id = "titleLabel"

                    label(
                        "Hello, ${values["inputId"]}",
                        LabelStyle.HEADLINE2,
                        textColor = ForegroundColor.PRIMARY
                    )
                }
            })
        )
}