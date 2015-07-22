window.selectedCat=""

window.confirmDeletion = (id) ->
	if confirm "Are you sure to want to delete this cat?"
		return $.ajax 
			url: '/cat/'+id,
			type: 'DELETE',
			success: (data) -> window.location.replace '/',
			error: (data) -> alert(JSON.stringify(data))


window.catSelected = (id) ->
    if id is window.selectedCat 
    then window.location.replace '/'
    else
        $("div#catInfo_"+window.selectedCat).css("background-color","white");
        window.selectedCat = id
        $("div#catInfo_"+id).css("background-color","#CCFFFF");
        $.get "/cat/"+id, (cat) ->
            if $(cat).length
                $("input#id").val cat.id
                $("input#name").val cat.name
                $("input#age").val cat.age
                $("input#color").val cat.color
                $("input#picture").val cat.picture
                $("input#race").val cat.race
                $("input#gender_"+cat.gender).prop("checked", true)
                $("input:submit").val "Modify a Cat"

$ ->
	$.get "/allcatdetails", (cats) ->
		$("#cats div:first").remove()
		if $(cats).length
		  $("#cats").append(cats)
