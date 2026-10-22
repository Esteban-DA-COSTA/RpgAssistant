package com.estebandacosta.jdrassistant.creation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.estebandacosta.jdrassistant.LocalDialogParent
import com.estebandacosta.jdrassistant.components.GemButton
import com.estebandacosta.jdrassistant.components.JdrButton
import com.estebandacosta.jdrassistant.components.JdrText
import com.estebandacosta.jdrassistant.components.Separator
import com.estebandacosta.jdrassistant.storage.WorldStorage
import com.estebandacosta.jdrassistant.storage.WorldStorageError
import com.estebandacosta.jdrassistant.theme.FantasyIcons
import com.estebandacosta.jdrassistant.theme.JdrColors
import com.estebandacosta.jdrassistant.theme.JdrTheme
import com.estebandacosta.jdrassistant.theme.panel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import kotlinx.coroutines.launch
import java.nio.file.Path

/** Modale de création d'un monde : [onCreated] reçoit l'archive `.rpga` écrite, [onDismiss] ferme sans rien créer. */
@Composable
fun CreateWorldDialog(onDismiss: () -> Unit, onCreated: (Path) -> Unit) {
    var form by remember { mutableStateOf(CreateWorldForm()) }
    var errors by remember { mutableStateOf(emptySet<CreateWorldError>()) }
    var storageError by remember { mutableStateOf<WorldStorageError?>(null) }
    val scope = rememberCoroutineScope()
    val directoryPicker = rememberDirectoryPickerLauncher(
        directory = form.location?.let { PlatformFile(it.toFile()) },
        dialogSettings = FileKitDialogSettings(title = "Emplacement du monde", parent = LocalDialogParent.current),
    ) { picked ->
        if (picked != null) {
            form = form.copy(location = picked.file.toPath())
            errors -= CreateWorldError.MissingLocation
            storageError = null
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, scrimColor = JdrTheme.colors.scrim),
    ) {
        CreateWorldCard(
            form = form,
            nameError = nameErrorMessage(errors, storageError),
            locationError = locationErrorMessage(errors, storageError),
            onNameChange = {
                form = form.copy(name = it)
                errors = errors.filterNot { error -> error is CreateWorldError.Name }.toSet()
                storageError = null
            },
            onBrowse = { directoryPicker.launch() },
            onCancel = onDismiss,
            onSubmit = {
                form.validate().fold(
                    { errors = it.toSet() },
                    { world -> scope.launch { WorldStorage.create(world).fold({ storageError = it }, onCreated) } },
                )
            },
        )
    }
}

private fun nameErrorMessage(errors: Set<CreateWorldError>, storageError: WorldStorageError?): String? =
    if (storageError is WorldStorageError.AlreadyExists) {
        "Un monde « ${storageError.archive.fileName} » existe déjà dans ce dossier."
    } else {
        when (val error = errors.filterIsInstance<CreateWorldError.Name>().firstOrNull()) {
            CreateWorldError.BlankName -> "Donnez un nom à votre monde."
            is CreateWorldError.ForbiddenCharacters ->
                "Un nom de fichier ne peut pas contenir : ${error.characters.joinToString(" ")}"
            null -> null
        }
    }

private fun locationErrorMessage(errors: Set<CreateWorldError>, storageError: WorldStorageError?): String? =
    when {
        storageError is WorldStorageError.WriteFailed -> "Impossible d'écrire dans ce dossier."
        CreateWorldError.MissingLocation in errors -> "Choisissez le dossier où enregistrer le monde."
        else -> null
    }

@Composable
private fun CreateWorldCard(
    form: CreateWorldForm,
    nameError: String?,
    locationError: String?,
    onNameChange: (String) -> Unit,
    onBrowse: () -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
) {
    val colors = JdrTheme.colors
    val typography = JdrTheme.typography
    Box(Modifier.padding(24.dp).widthIn(max = 600.dp).fillMaxWidth()) {
        Column(
            Modifier
                .padding(top = 26.dp)
                .panel(colors, RoundedCornerShape(12.dp), rim = colors.rimStrong, elevation = 24.dp)
                .padding(start = 40.dp, end = 40.dp, top = 52.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            JdrText(
                "Donnez un nom à votre monde et choisissez l'endroit où ses données seront conservées.",
                typography.description.copy(textAlign = TextAlign.Center),
                colors.textMuted,
                Modifier.fillMaxWidth(),
            )
            FormField(
                label = "Nom du monde",
                hint = null,
                error = nameError,
            ) {
                NameInput(form.name, onNameChange)
            }
            FormField(
                label = "Emplacement des données",
                hint = "Le monde et toutes ses fiches seront enregistrés dans ce dossier, en un seul fichier .rpga.",
                error = locationError,
            ) {
                LocationInput(form.location, onBrowse)
            }
            Separator()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.End)) {
                JdrButton("Annuler", onCancel)
                JdrButton("Créer le monde", onSubmit, primary = true)
            }
        }
        TitlePlate(Modifier.align(Alignment.TopCenter))
        CloseButton(onCancel, Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 14.dp))
    }
}

@Composable
private fun TitlePlate(modifier: Modifier) {
    val colors = JdrTheme.colors
    Box(
        modifier
            .panel(colors, RoundedCornerShape(8.dp), rim = colors.rimStrong)
            .padding(horizontal = 44.dp, vertical = 10.dp),
    ) {
        JdrText("Créer un monde", JdrTheme.typography.heading, colors.textHighlight)
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier) {
    val colors = JdrTheme.colors
    Box(
        modifier
            .size(44.dp)
            .border(1.dp, colors.rim, CircleShape)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(colors.panelTop, colors.panelBottom)))
            .clickable(role = Role.Button, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            FantasyIcons.close,
            contentDescription = "Fermer",
            Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(colors.brand),
        )
    }
}

/** Champ de formulaire : libellé, contenu, puis l'erreur si elle existe, sinon l'aide. */
@Composable
private fun FormField(label: String, hint: String?, error: String?, content: @Composable () -> Unit) {
    val colors = JdrTheme.colors
    val typography = JdrTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        JdrText(label, typography.label, colors.brand)
        content()
        when {
            error != null -> JdrText(error, typography.footnote, colors.danger)
            hint != null -> JdrText(hint, typography.footnote, colors.textMuted)
        }
    }
}

@Composable
private fun NameInput(name: String, onNameChange: (String) -> Unit) {
    val colors = JdrTheme.colors
    val style = JdrTheme.typography.input
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BasicTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused },
        singleLine = true,
        textStyle = style.copy(color = colors.textStrong),
        cursorBrush = SolidColor(colors.textStrong),
        decorationBox = { innerTextField ->
            Box(
                Modifier.fieldFrame(colors, if (focused) colors.rim else colors.rimDim).padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (name.isEmpty()) JdrText("Ex. Royaumes de Valdor", style, colors.textFaint)
                innerTextField()
            }
        },
    )
}

@Composable
private fun LocationInput(location: Path?, onBrowse: () -> Unit) {
    val colors = JdrTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.weight(1f).fieldFrame(colors, colors.rimDim).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            JdrText(
                location?.toString() ?: "Aucun dossier choisi",
                JdrTheme.typography.mono,
                if (location == null) colors.textFaint else colors.textStrong,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )
        }
        GemButton("Parcourir…", JdrTheme.gems.tertiary, FantasyIcons.folder, onClick = onBrowse)
    }
}

private val FieldShape = RoundedCornerShape(6.dp)

private fun Modifier.fieldFrame(colors: JdrColors, border: Color): Modifier = this
    .fillMaxWidth()
    .height(52.dp)
    .background(colors.field, FieldShape)
    .border(1.dp, border, FieldShape)
