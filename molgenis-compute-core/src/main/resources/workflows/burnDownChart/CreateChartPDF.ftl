#string points
#string team
#list day
#list month
#list dayName
#string sprint

#FOREACH points

bash -c "echo '\documentclass[a4paper]{article}
\usepackage[left=1cm,top=1cm,right=1cm,bottom=1cm]{geometry}
\usepackage{graphics}
\usepackage{xcolor}
\usepackage{tabu}

\begin{document}
\thispagestyle{empty}

\begin{table}
\resizebox{\textwidth}{.5\textheight}{%
\taburulecolor{gray}

\begin{tabu}{|r|<#list 1..day?size as i>c|</#list>}
    \multicolumn{${day?size+1}}{c}{\large Burn down chart team \textit{\textbf{${team}}}, sprint {\bf ${sprint}}. Points/day: {\bf ${points?number/day?size}}.} \\\\
    \hline
	<#list points?number..0 as p>
	\hfill ${p} <#list 1..day?size as i>& ~</#list> \\\\
	\hline
	</#list>
	~ <#list day as d>& <#if d?number < 10>~</#if>${d}/${month[d_index]}</#list> \\\\ \hline
	~ <#list dayName as d>& ${d}</#list> \\\\ \hline
\end{tabu}%
}
\end{table}

\end{document}' > ${team}Chart.tex"

mkdir -p chart
pdflatex -output-directory=chart ${team}Chart.tex
pdflatex -output-directory=chart ${team}Chart.tex

open chart/${team}Chart.pdf