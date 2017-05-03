<#import "layout/main.ftl" as layout>
<#import "widget/table.ftl" as widget>

<@layout.main>
<div class="page-header">
	<h2>Overview</h2>
</div>

<div class="row">
	<div class="col-md-6">
		<h4>Ausgaben</h4>
		<table class="table">
			<tbody>
				<tr>
					<td>${context.output.total}</td>
				<tr>
			</tbody>
		</table>
	</div>
	<div class="col-md-6">
		<h4>Einnahmen</h4>
		<table class="table">
			<tbody>
				<tr>
					<td>${context.input.total}</td>
				<tr>
			</tbody>
		</table>
	</div>
</div>

<div class="row">
	<div class="col-md-6">
		<@widget.table context.output.items/>
	</div>

	<div class="col-md-6">
        <@widget.table context.input.items/>
	</div>
</div>
</@layout.main>