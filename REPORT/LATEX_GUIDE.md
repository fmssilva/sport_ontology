# 📝 LaTeX Report Guide

## 🚀 Setup Steps

### 1. Install VS Code Extension
```
james-yu.latex-workshop
```

### 2. Install LaTeX and Perl
```powershell
# Install MiKTeX (if not already installed)
choco install miktex -y

# Install Strawberry Perl (required for latexmk)
choco install strawberryperl -y

# Add Perl to PATH (if VS Code can't find it)
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;C:\Strawberry\perl\bin", "User")
```

### 3. Edit LaTeX File
- Open `sport_ontology_report.tex` in VS Code
- Edit your content

### 4. Preview (Live)
- Click ▶️ **Play button** in VS Code
- PDF preview opens automatically

### 5. Export Final PDF
```cmd
latexmk -pdf sport_ontology_report.tex
```

## 📝 LaTeX Basics

### Adding Content
```latex
\section{New Section}
Your content here...

\subsection{Subsection}
More content...
```

### Adding Images
```latex
\begin{figure}
\centering
\includegraphics[width=0.8\textwidth]{images/your_image.png}
\caption{Your caption}
\label{fig:your_label}
\end{figure}
```

### Adding Tables
```latex
\begin{table}
\caption{Table Title}
\begin{tabular}{|l|c|r|}
\hline
Left & Center & Right \\
\hline
Data1 & Data2 & Data3 \\
\hline
\end{tabular}
\end{table}
```

### Math Equations
```latex
\begin{equation}
E = mc^2
\end{equation}

Inline math: $x = y + z$
```

### Code Blocks
```latex
\begin{minted}[frame=lines, bgcolor=white, fontsize=\small, linenos]{sql}
SELECT * FROM Player WHERE market_value > 50000000;
\end{minted}
```


## ⚡ Workflow

1. **Edit** `sport_ontology_report.tex` in VS Code
2. **Save** your changes
3. **Run** `compile_latex.bat`
4. **View** the generated PDF
5. **Repeat** until satisfied

## 🐛 Common Issues

**Error: "Package not found"**
- Solution: Install missing packages via MiKTeX Package Manager

**Error: "Image not found"**
- Solution: Check image path and file exists in `images/` folder

**Error: "Compilation failed"**
- Solution: Check the `.log` file for detailed error messages

