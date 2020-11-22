<template>
    <div class="about">
        <h1 @click="click" v-if="show" :id="dynamicId">This is an about page{{name + 1}}</h1>
        {{price}}
    </div>
</template>
<script>

export default {
    name: 'home',
    data () {
        return {
            name: 123,
            dynamicId: 111,
            show: true,
            a: 1,
            b: 2
        }
    },
    methods: {
        click () {
            console.log('click')
        }
    },
    computed: {
        price () {
            return this.a + this.b
        }
    }
}
</script>
