package com.example.template.config

import com.example.template.ForgeTemplate
import gg.essential.vigilance.Vigilant
import java.io.File

object TemplateConfig : Vigilant(File(ForgeTemplate.modDir, "${ForgeTemplate.ID}.toml"), ForgeTemplate.NAME) {

    init {
        initialize()
    }
}